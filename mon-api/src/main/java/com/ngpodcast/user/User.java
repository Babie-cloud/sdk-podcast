package com.ngpodcast.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean anonymous = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // ── Constructeurs ────────────────────────────────────────────────────────
    public User() {}

    public User(String id, String name, String prenom, String username,
                String email, String password, Role role,
                boolean anonymous, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.prenom = prenom;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.anonymous = anonymous;
        this.createdAt = createdAt;
    }

    // ── Builder statique ─────────────────────────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String name;
        private String prenom;
        private String username;
        private String email;
        private String password;
        private Role role = Role.USER;
        private boolean anonymous = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(String id)             { this.id = id; return this; }
        public Builder name(String name)         { this.name = name; return this; }
        public Builder prenom(String prenom)     { this.prenom = prenom; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder email(String email)       { this.email = email; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(Role role)           { this.role = role; return this; }
        public Builder anonymous(boolean a)      { this.anonymous = a; return this; }

        public User build() {
            User u = new User();
            u.id        = this.id;
            u.name      = this.name;
            u.prenom    = this.prenom;
            u.username  = this.username;
            u.email     = this.email;
            u.password  = this.password;
            u.role      = this.role;
            u.anonymous = this.anonymous;
            u.createdAt = this.createdAt;
            return u;
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getId()            { return id; }
    public String getName()          { return name; }
    public String getPrenom()        { return prenom; }
    public Role   getRole()          { return role; }
    public boolean isAnonymous()     { return anonymous; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(String id)           { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setPrenom(String prenom)   { this.prenom = prenom; }
    public void setUsername(String u)      { this.username = u; }
    public void setEmail(String email)     { this.email = email; }
    public void setPassword(String pwd)    { this.password = pwd; }
    public void setRole(Role role)         { this.role = role; }
    public void setAnonymous(boolean a)    { this.anonymous = a; }

    // ── UserDetails ──────────────────────────────────────────────────────────
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override public String  getPassword()               { return password; }
    @Override public String  getUsername()               { return email; }
    @Override public boolean isAccountNonExpired()       { return true; }
    @Override public boolean isAccountNonLocked()        { return true; }
    @Override public boolean isCredentialsNonExpired()   { return true; }
    @Override public boolean isEnabled()                 { return true; }
}
