package model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Transaction {
    final private Logger logger = LoggerFactory.getLogger(Transaction.class);
    public void deposit(User user, double amount){
        if (amount > 0) {
        user.deposit(amount);
        logger.info("Nạp thành công {} vào tài khoản của {}", amount, user.getUsername());
        }
        else {
            logger.error("Không thể thực hiện giao dịch!");
        }
    }
    public void withdraw(User user, double amount){
        if (amount > 0 && user.getBalance() >= amount) {
            user.withdraw(amount);
            logger.info("Rút thành công {} từ tài khoản của {}", amount, user.getUsername());
        }
        else {
        logger.error("Không thể thực hiện giao dịch!");
        }
    }
}
