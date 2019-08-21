import com.google.inject.AbstractModule;
import services.BankService;
import services.FinanceService;
import services.UserService;
import services.impl.BankServiceImpl;
import services.impl.FinanceServiceImpl;
import services.impl.UserServiceImpl;

public class Module extends AbstractModule {
    @Override
    public void configure() {
        bind(UserService.class).to(UserServiceImpl.class);
        bind(BankService.class).to(BankServiceImpl.class);
        bind(FinanceService.class).to(FinanceServiceImpl.class);
    }
}