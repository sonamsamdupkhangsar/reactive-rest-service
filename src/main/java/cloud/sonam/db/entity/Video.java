package cloud.sonam.db.entity;

import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Video {
    @Id
    private Long id;

    private String path;

    private LocalDateTime stored = LocalDateTime.now();

    public Video() {

    }

    public Video(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video myVideo = (Video) o;
        return Objects.equals(id, myVideo.id) && Objects.equals(path, myVideo.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path);
    }

    @Override
    public String toString() {
        return "MyFile{" +
                "id=" + id +
                ", path='" + path + '\'' +
                '}';
    }
}
