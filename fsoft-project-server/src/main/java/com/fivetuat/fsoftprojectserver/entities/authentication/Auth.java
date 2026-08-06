package com.fivetuat.fsoftprojectserver.entities.authentication;

import com.fivetuat.fsoftprojectserver.entities.Profile;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "auths")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(unique = true, nullable = false)
    String email;
    String phone;
//    @Column(nullable = false)
    String passwordHash;
    @OneToOne(mappedBy = "auth", cascade = CascadeType.ALL)
    Profile profile;
    @ManyToMany(fetch = FetchType.LAZY)
            @JoinTable(
                    name = "auth_roles",
                    joinColumns = @JoinColumn(name = "auth_id"),
                    inverseJoinColumns = @JoinColumn(name = "role_id")
            )
    Set<Role> roles;
}
