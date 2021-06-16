package cloud.sonam.db.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Video {
    @Id
    private Long id;

    private String name;

    private String thumb;

    private String path;

    private LocalDateTime stored = LocalDateTime.now();

    public Video() {

    }

    public Video(String name, String path, String thumb) {
        this.name = name;
        this.path = path;
        this.thumb = thumb;
    }

    public Long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getThumb() {
        return thumb;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return Objects.equals(id, video.id) && Objects.equals(getPath(), video.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getPath());
    }

    @Override
    public String toString() {
        return "Video{" +
                "id=" + id +
                ", thumb='" + thumb + '\'' +
                ", path='" + path + '\'' +
                ", stored=" + stored +
                ", name=" + name +
                '}';
    }
}
