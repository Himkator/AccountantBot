package io.proj3ect.accontantbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "mail_now")
public class MailNow {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long chatId;
    @Column(length = 2550000)
    private String body;
    private Timestamp date;
    private String photo;
    private String channel;
    private String doc;
    private String DocPath;
    private String picPath;
    @Column(length = 25500)
    private String title;
}
