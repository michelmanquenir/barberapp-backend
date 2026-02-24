package barberiapp.controller;

import barberiapp.model.Transaction;
import barberiapp.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@RequestParam String userId) {
        return walletService.getUserTransactions(userId);
    }

    @GetMapping("/balance")
    public Map<String, Integer> getBalance(@RequestParam String userId) {
        Integer balance = walletService.getUserBalance(userId);
        return Map.of("balance", balance);
    }

    @PostMapping("/add-funds")
    public Transaction addFunds(@RequestParam String userId, @RequestBody Map<String, Object> body) {
        Integer amount = (Integer) body.get("amount");
        return walletService.addFunds(userId, amount, "Recarga de Saldo");
    }
}
