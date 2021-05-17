package cloud.sonam.db.entity;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public class User {
    @Id
    private UUID uuid;

    private String firstName;
    private String lastName;

    public User() {

    }
    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }


}
