package in.ashar.mooble.security;

import in.ashar.mooble.entity.Credentials2;
import in.ashar.mooble.repository.CredentialsRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final CredentialsRepository credentialsRepository;

    public CustomUserDetailsService(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Credentials2 user = credentialsRepository.findByEmail(email)
                  .orElseThrow(() ->{
                      System.out.println("User not found with email: " + email);
                      return new UsernameNotFoundException("User not found with email: " + email);
                  });

        // Map Role -> GrantedAuthority (prefix ROLE_)
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());


        // Use Spring's User for UserDetails (includes username, password, authorities)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() == null ? "" : user.getPassword()) // empty if not set
                .authorities(authority)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
