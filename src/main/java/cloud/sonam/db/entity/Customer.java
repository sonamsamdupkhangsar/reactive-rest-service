package cloud.sonam.db.entity;

import lombok.AllArgsConstructor;
import lombok.Data;


import lombok.With;
import lombok.experimental.Wither;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@With
public class Customer {

    @Id public Long id;
    private String firstname, lastname;

    public boolean hasId() {
        return id != null;
    }

}