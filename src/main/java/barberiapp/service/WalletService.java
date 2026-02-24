package barberiapp.service;

import barberiapp.model.Transaction;
import barberiapp.model.Profile;
import barberiapp.repository.TransactionRepository;
import barberiapp.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final TransactionRepository transactionRepository;
    private final ProfileRepository profileRepository;

    public List<Transaction> getUserTransactions(String userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    // Calcula el balance basado en las transacciones de credito y debito
    public Integer getUserBalance(String userId) {
        List<Transaction> transactions = getUserTransactions(userId);
        return transactions.stream()
                .mapToInt(t -> "credit".equals(t.getType()) ? t.getAmount() : -t.getAmount())
                .sum();
    }

    public Transaction addFunds(String userId, Integer amount, String description) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Transaction t = new Transaction();
        t.setUser(profile);
        t.setType("credit");
        t.setAmount(amount);
        t.setDescription(description);
        return transactionRepository.save(t);
    }
}
