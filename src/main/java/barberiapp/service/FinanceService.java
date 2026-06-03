package barberiapp.service;

import barberiapp.model.*;
import barberiapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final FinanceIncomeRepository incomeRepo;
    private final FinanceExpenseRepository expenseRepo;
    private final FinanceInstallmentRepository installmentRepo;
    private final FinanceSavingGoalRepository savingGoalRepo;
    private final ProfileRepository profileRepo;

    // ── Summary ─────────────────────────────────────────────────────────────

    public Map<String, Object> getSummary(String userId) {
        LocalDate now  = LocalDate.now();
        LocalDate from = now.withDayOfMonth(1);
        LocalDate to   = now.withDayOfMonth(now.lengthOfMonth());

        List<FinanceExpense> allExpenses = expenseRepo.findByUserIdOrderByDateDesc(userId);
        List<FinanceIncome>  allIncomes  = incomeRepo.findByUserIdOrderByDateDesc(userId);

        // ── Gastos ──────────────────────────────────────────────────────────
        double monthlyExpenses = 0;
        Map<String, Double> expenseMap = new HashMap<>();

        // 1. Gastos normales (no periódicos, no cuotas) del mes actual
        for (FinanceExpense e : allExpenses) {
            if (e.getInstallmentNumber() != null) continue;
            if (Boolean.TRUE.equals(e.getRecurring())) continue;
            if (e.getDate().isBefore(from) || e.getDate().isAfter(to)) continue;
            monthlyExpenses += e.getAmount();
            expenseMap.merge(e.getCategory(), e.getAmount(), Double::sum);
        }

        // 2. Gastos periódicos: se cuentan siempre a partir del mes en que se registraron
        for (FinanceExpense e : allExpenses) {
            if (!Boolean.TRUE.equals(e.getRecurring())) continue;
            if (e.getDate().isAfter(to)) continue;
            monthlyExpenses += e.getAmount();
            expenseMap.merge(e.getCategory(), e.getAmount(), Double::sum);
        }

        // 3. Gastos en cuotas: para cada plan activo, contar una cuota mensual.
        //    - Si el usuario ya registró la cuota de este mes → contar esa.
        //    - Si no la registró aún pero el plan sigue activo → contar el monto igual.
        Map<String, FinanceExpense> latestByPlan       = new HashMap<>();
        Map<String, FinanceExpense> currentMonthByPlan = new HashMap<>();

        for (FinanceExpense e : allExpenses) {
            if (e.getInstallmentNumber() == null) continue;
            if (e.getDate().isAfter(to)) continue;

            String key = e.getCategory() + "|" + e.getInstallmentTotal() + "|"
                       + (e.getDescription() != null ? e.getDescription() : "");

            FinanceExpense existing = latestByPlan.get(key);
            if (existing == null || e.getInstallmentNumber() > existing.getInstallmentNumber()) {
                latestByPlan.put(key, e);
            }

            if (!e.getDate().isBefore(from) && !e.getDate().isAfter(to)) {
                FinanceExpense cur = currentMonthByPlan.get(key);
                if (cur == null || e.getInstallmentNumber() > cur.getInstallmentNumber()) {
                    currentMonthByPlan.put(key, e);
                }
            }
        }

        for (String key : latestByPlan.keySet()) {
            FinanceExpense latest = latestByPlan.get(key);
            if (latest.getInstallmentNumber() >= latest.getInstallmentTotal()) continue; // plan completado

            FinanceExpense toCount = currentMonthByPlan.containsKey(key)
                    ? currentMonthByPlan.get(key)  // cuota ya registrada este mes
                    : latest;                       // plan activo, cuota aún no registrada

            monthlyExpenses += toCount.getAmount();
            expenseMap.merge(toCount.getCategory(), toCount.getAmount(), Double::sum);
        }

        // ── Ingresos ─────────────────────────────────────────────────────────
        double monthlyIncome = 0;
        Map<String, Double> incomeMap = new HashMap<>();

        // 1. Ingresos no periódicos del mes actual
        for (FinanceIncome i : allIncomes) {
            if (Boolean.TRUE.equals(i.getRecurring())) continue;
            if (i.getDate().isBefore(from) || i.getDate().isAfter(to)) continue;
            monthlyIncome += i.getAmount();
            incomeMap.merge(i.getType(), i.getAmount(), Double::sum);
        }

        // 2. Ingresos periódicos: se cuentan siempre a partir del mes en que se registraron
        for (FinanceIncome i : allIncomes) {
            if (!Boolean.TRUE.equals(i.getRecurring())) continue;
            if (i.getDate().isAfter(to)) continue;
            monthlyIncome += i.getAmount();
            incomeMap.merge(i.getType(), i.getAmount(), Double::sum);
        }

        // ── Resultado ────────────────────────────────────────────────────────
        Double pendingInstallments = installmentRepo.sumPendingAmountByUserId(userId);
        Double totalSavings        = savingGoalRepo.sumCurrentAmountByUserId(userId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("monthlyIncome",        monthlyIncome);
        summary.put("monthlyExpenses",      monthlyExpenses);
        summary.put("monthlyBalance",       monthlyIncome - monthlyExpenses);
        summary.put("pendingInstallments",  pendingInstallments != null ? pendingInstallments : 0);
        summary.put("totalSavings",         totalSavings != null ? totalSavings : 0);
        summary.put("expensesByCategory",   expenseMap);
        summary.put("incomesByType",        incomeMap);
        return summary;
    }

    // ── Incomes ─────────────────────────────────────────────────────────────

    public List<FinanceIncome> getIncomes(String userId) {
        return incomeRepo.findByUserIdOrderByDateDesc(userId);
    }

    public FinanceIncome createIncome(String userId, FinanceIncome income) {
        Profile profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        income.setUser(profile);
        income.setId(null);
        return incomeRepo.save(income);
    }

    public FinanceIncome updateIncome(Long id, String userId, FinanceIncome data) {
        FinanceIncome existing = incomeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        existing.setType(data.getType());
        existing.setDescription(data.getDescription());
        existing.setAmount(data.getAmount());
        existing.setDate(data.getDate());
        existing.setRecurring(data.getRecurring() != null ? data.getRecurring() : false);
        existing.setDurationMonths(data.getDurationMonths());
        return incomeRepo.save(existing);
    }

    public void deleteIncome(Long id, String userId) {
        FinanceIncome existing = incomeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingreso no encontrado"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        incomeRepo.delete(existing);
    }

    // ── Expenses ─────────────────────────────────────────────────────────────

    public List<FinanceExpense> getExpenses(String userId) {
        return expenseRepo.findByUserIdOrderByDateDesc(userId);
    }

    public FinanceExpense createExpense(String userId, FinanceExpense expense) {
        Profile profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        expense.setUser(profile);
        expense.setId(null);
        return expenseRepo.save(expense);
    }

    public FinanceExpense updateExpense(Long id, String userId, FinanceExpense data) {
        FinanceExpense existing = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        existing.setCategory(data.getCategory());
        existing.setDescription(data.getDescription());
        existing.setAmount(data.getAmount());
        existing.setDate(data.getDate());
        existing.setRecurring(data.getRecurring() != null ? data.getRecurring() : false);
        existing.setInstallmentNumber(data.getInstallmentNumber());
        existing.setInstallmentTotal(data.getInstallmentTotal());
        return expenseRepo.save(existing);
    }

    public void deleteExpense(Long id, String userId) {
        FinanceExpense existing = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Gasto no encontrado"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        expenseRepo.delete(existing);
    }

    // ── Installments ─────────────────────────────────────────────────────────

    public List<FinanceInstallment> getInstallments(String userId) {
        return installmentRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public FinanceInstallment createInstallment(String userId, FinanceInstallment installment) {
        Profile profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        installment.setUser(profile);
        installment.setId(null);
        return installmentRepo.save(installment);
    }

    public FinanceInstallment updateInstallment(Long id, String userId, FinanceInstallment data) {
        FinanceInstallment existing = installmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        existing.setDescription(data.getDescription());
        existing.setTotalAmount(data.getTotalAmount());
        existing.setInstallmentAmount(data.getInstallmentAmount());
        existing.setTotalInstallments(data.getTotalInstallments());
        existing.setPaidInstallments(data.getPaidInstallments());
        existing.setDueDay(data.getDueDay());
        return installmentRepo.save(existing);
    }

    public FinanceInstallment payInstallment(Long id, String userId) {
        FinanceInstallment existing = installmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        if (existing.getPaidInstallments() < existing.getTotalInstallments()) {
            existing.setPaidInstallments(existing.getPaidInstallments() + 1);
        }
        return installmentRepo.save(existing);
    }

    public void deleteInstallment(Long id, String userId) {
        FinanceInstallment existing = installmentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuota no encontrada"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        installmentRepo.delete(existing);
    }

    // ── Saving Goals ─────────────────────────────────────────────────────────

    public List<FinanceSavingGoal> getSavingGoals(String userId) {
        return savingGoalRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public FinanceSavingGoal createSavingGoal(String userId, FinanceSavingGoal goal) {
        Profile profile = profileRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        goal.setUser(profile);
        goal.setId(null);
        return savingGoalRepo.save(goal);
    }

    public FinanceSavingGoal updateSavingGoal(Long id, String userId, FinanceSavingGoal data) {
        FinanceSavingGoal existing = savingGoalRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta no encontrada"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        existing.setName(data.getName());
        existing.setTargetAmount(data.getTargetAmount());
        existing.setCurrentAmount(data.getCurrentAmount());
        existing.setTargetDate(data.getTargetDate());
        return savingGoalRepo.save(existing);
    }

    public FinanceSavingGoal addToSavingGoal(Long id, String userId, Double amount) {
        FinanceSavingGoal existing = savingGoalRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta no encontrada"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        existing.setCurrentAmount(existing.getCurrentAmount() + amount);
        return savingGoalRepo.save(existing);
    }

    public void deleteSavingGoal(Long id, String userId) {
        FinanceSavingGoal existing = savingGoalRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta no encontrada"));
        if (!existing.getUser().getId().equals(userId))
            throw new RuntimeException("Sin permisos");
        savingGoalRepo.delete(existing);
    }
}
