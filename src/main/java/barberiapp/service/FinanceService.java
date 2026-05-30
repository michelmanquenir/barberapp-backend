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
        LocalDate now = LocalDate.now();
        LocalDate from = now.withDayOfMonth(1);
        LocalDate to = now.withDayOfMonth(now.lengthOfMonth());

        Double monthlyIncome = incomeRepo.sumByUserIdAndDateBetween(userId, from, to);
        Double monthlyExpenses = expenseRepo.sumByUserIdAndDateBetween(userId, from, to);
        Double pendingInstallments = installmentRepo.sumPendingAmountByUserId(userId);
        Double totalSavings = savingGoalRepo.sumCurrentAmountByUserId(userId);

        List<Object[]> expensesByCategory = expenseRepo.sumByCategoryAndPeriod(userId, from, to);
        List<Object[]> incomesByType = incomeRepo.sumByTypeAndPeriod(userId, from, to);

        Map<String, Double> expenseMap = new HashMap<>();
        for (Object[] row : expensesByCategory) {
            expenseMap.put((String) row[0], (Double) row[1]);
        }

        Map<String, Double> incomeMap = new HashMap<>();
        for (Object[] row : incomesByType) {
            incomeMap.put((String) row[0], (Double) row[1]);
        }

        double safeIncome = monthlyIncome != null ? monthlyIncome : 0;
        double safeExpenses = monthlyExpenses != null ? monthlyExpenses : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("monthlyIncome", safeIncome);
        summary.put("monthlyExpenses", safeExpenses);
        summary.put("monthlyBalance", safeIncome - safeExpenses);
        summary.put("pendingInstallments", pendingInstallments != null ? pendingInstallments : 0);
        summary.put("totalSavings", totalSavings != null ? totalSavings : 0);
        summary.put("expensesByCategory", expenseMap);
        summary.put("incomesByType", incomeMap);
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
