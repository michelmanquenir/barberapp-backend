package barberiapp.controller;

import barberiapp.model.*;
import barberiapp.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    // ── Summary ───────────────────────────────────────────────────────────────

    @GetMapping("/summary")
    public Map<String, Object> getSummary(@RequestParam String userId) {
        return financeService.getSummary(userId);
    }

    // ── Incomes ───────────────────────────────────────────────────────────────

    @GetMapping("/incomes")
    public List<FinanceIncome> getIncomes(@RequestParam String userId) {
        return financeService.getIncomes(userId);
    }

    @PostMapping("/incomes")
    public FinanceIncome createIncome(@RequestParam String userId,
                                      @RequestBody FinanceIncome income) {
        return financeService.createIncome(userId, income);
    }

    @PutMapping("/incomes/{id}")
    public FinanceIncome updateIncome(@PathVariable Long id,
                                      @RequestParam String userId,
                                      @RequestBody FinanceIncome income) {
        return financeService.updateIncome(id, userId, income);
    }

    @DeleteMapping("/incomes/{id}")
    public void deleteIncome(@PathVariable Long id, @RequestParam String userId) {
        financeService.deleteIncome(id, userId);
    }

    // ── Expenses ──────────────────────────────────────────────────────────────

    @GetMapping("/expenses")
    public List<FinanceExpense> getExpenses(@RequestParam String userId) {
        return financeService.getExpenses(userId);
    }

    @PostMapping("/expenses")
    public FinanceExpense createExpense(@RequestParam String userId,
                                        @RequestBody FinanceExpense expense) {
        return financeService.createExpense(userId, expense);
    }

    @PutMapping("/expenses/{id}")
    public FinanceExpense updateExpense(@PathVariable Long id,
                                        @RequestParam String userId,
                                        @RequestBody FinanceExpense expense) {
        return financeService.updateExpense(id, userId, expense);
    }

    @DeleteMapping("/expenses/{id}")
    public void deleteExpense(@PathVariable Long id, @RequestParam String userId) {
        financeService.deleteExpense(id, userId);
    }

    // ── Installments ──────────────────────────────────────────────────────────

    @GetMapping("/installments")
    public List<FinanceInstallment> getInstallments(@RequestParam String userId) {
        return financeService.getInstallments(userId);
    }

    @PostMapping("/installments")
    public FinanceInstallment createInstallment(@RequestParam String userId,
                                                @RequestBody FinanceInstallment installment) {
        return financeService.createInstallment(userId, installment);
    }

    @PutMapping("/installments/{id}")
    public FinanceInstallment updateInstallment(@PathVariable Long id,
                                                @RequestParam String userId,
                                                @RequestBody FinanceInstallment installment) {
        return financeService.updateInstallment(id, userId, installment);
    }

    @PatchMapping("/installments/{id}/pay")
    public FinanceInstallment payInstallment(@PathVariable Long id,
                                             @RequestParam String userId) {
        return financeService.payInstallment(id, userId);
    }

    @DeleteMapping("/installments/{id}")
    public void deleteInstallment(@PathVariable Long id, @RequestParam String userId) {
        financeService.deleteInstallment(id, userId);
    }

    // ── Saving Goals ──────────────────────────────────────────────────────────

    @GetMapping("/saving-goals")
    public List<FinanceSavingGoal> getSavingGoals(@RequestParam String userId) {
        return financeService.getSavingGoals(userId);
    }

    @PostMapping("/saving-goals")
    public FinanceSavingGoal createSavingGoal(@RequestParam String userId,
                                              @RequestBody FinanceSavingGoal goal) {
        return financeService.createSavingGoal(userId, goal);
    }

    @PutMapping("/saving-goals/{id}")
    public FinanceSavingGoal updateSavingGoal(@PathVariable Long id,
                                              @RequestParam String userId,
                                              @RequestBody FinanceSavingGoal goal) {
        return financeService.updateSavingGoal(id, userId, goal);
    }

    @PatchMapping("/saving-goals/{id}/add")
    public FinanceSavingGoal addToSavingGoal(@PathVariable Long id,
                                             @RequestParam String userId,
                                             @RequestBody Map<String, Double> body) {
        return financeService.addToSavingGoal(id, userId, body.get("amount"));
    }

    @DeleteMapping("/saving-goals/{id}")
    public void deleteSavingGoal(@PathVariable Long id, @RequestParam String userId) {
        financeService.deleteSavingGoal(id, userId);
    }
}
