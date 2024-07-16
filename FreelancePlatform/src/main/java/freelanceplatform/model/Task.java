package freelanceplatform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne
    @JoinColumn(name = "freelancer_id")
    private User freelancer;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String problem;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Column(nullable = false)
    private Double payment;

    @Column
    private LocalDateTime assignedDate;

    @Column
    private LocalDateTime submittedDate;

    @Column
    private LocalDateTime postedDate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "solution_id")
    @JsonIgnore
    private Solution solution;

    public Task(User customer, String title, String problem, LocalDateTime deadline, Double payment, TaskType type) {
        this.customer = customer;
        this.title = title;
        this.problem = problem;
        this.deadline = deadline;
        this.status = TaskStatus.UNASSIGNED;
        this.payment = payment;
        this.type = type;
        this.postedDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Task{" +
                "customer=" + customer.getUsername() +
                ", freelancer=" + freelancer.getUsername() +
                ", title='" + title + '\'' +
                ", problem='" + problem + '\'' +
                ", deadline=" + deadline +
                ", status=" + status +
                ", payment=" + payment +
                ", assignedDate=" + assignedDate +
                ", submittedDate=" + submittedDate +
                ", postedDate=" + postedDate +
                '}';
    }
}
