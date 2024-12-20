package com.pichincha.ms.client.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("CLIENT")
public class Client {
    @Id
    private Long id;
    private String name;
    private String email;
    private String gender;
    private String status;
}
