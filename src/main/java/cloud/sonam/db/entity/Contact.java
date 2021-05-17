package cloud.sonam.db.entity;

import org.springframework.data.annotation.Id;

import java.util.Objects;
import java.util.UUID;

public class Contact {
    @Id
    private UUID uuid;
    private String email;

    private UUID userId;

    public Contact() {}

    public Contact(String email, UUID userId) {
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(uuid, contact.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "Contact{" +
                "uuid=" + uuid +
                ", email='" + email + '\'' +
                ", userId=" + userId +
                '}';
    }
}
