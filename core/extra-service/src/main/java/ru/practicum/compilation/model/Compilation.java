package ru.practicum.compilation.model;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.*;

import lombok.experimental.FieldDefaults;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "compilation")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String title;

    @Column Boolean pinned;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "compilation_events",
            joinColumns = @JoinColumn(name = "compilation_id"))
    @Column(name = "event_id")
    Set<Long> eventIds = new LinkedHashSet<>();
}
