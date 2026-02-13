package in.ashar.mooble.service;

import in.ashar.mooble.configuration.AppProperties;
import in.ashar.mooble.entity.Credentials2;
import in.ashar.mooble.entity.SubscriptionPlan;
import in.ashar.mooble.entity.TuitionCodeCounter;
import in.ashar.mooble.repository.CredentialsRepository;
import in.ashar.mooble.repository.SubscriptionPlanRepository;
import in.ashar.mooble.repository.TuitionCodeCounterRepository;
import in.ashar.mooble.utility.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InitializeTest implements CommandLineRunner {

    private final TuitionCodeCounterRepository counterRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final CredentialsRepository credentialsRepository;
    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args){

        List<TuitionCodeCounter> list = counterRepository.findAll();

        if(list.isEmpty()){
            TuitionCodeCounter counter = new TuitionCodeCounter(1, 100000);
            counterRepository.save(counter);
        }

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();

        if(plans.isEmpty()){

            List<SubscriptionPlan> sp = new ArrayList<>();
            SubscriptionPlan plan1 = new SubscriptionPlan();
            SubscriptionPlan plan2 = new SubscriptionPlan();
            SubscriptionPlan plan3 = new SubscriptionPlan();


            plan1.setName("FREE");
            plan1.setMaxMembers(15);
            plan1.setMaxStorageMb(100);
            plan1.setPricePerMonth(0);
            sp.add(plan1);

            plan2.setName("ELITE");
            plan2.setMaxMembers(50);
            plan2.setMaxStorageMb(500);
            plan2.setPricePerMonth(999);
            sp.add(plan2);

            plan3.setName("ENTERPRISE");
            plan3.setMaxMembers(200);
            plan3.setMaxStorageMb(2000);
            plan3.setPricePerMonth(4999);
            sp.add(plan3);


            subscriptionPlanRepository.saveAll(sp);
        }

        Optional<Credentials2> optCred = credentialsRepository.findByEmail(appProperties.getSuperUserEmail());

        if(optCred.isEmpty()){

            Credentials2 c = new Credentials2();
            c.setEmail(appProperties.getSuperUserEmail());
            c.setPassword(passwordEncoder.encode(appProperties.getSuperUserPass()));
            c.setRole(Role.SUPER_USER);

            credentialsRepository.save(c);
        }

    }

}
