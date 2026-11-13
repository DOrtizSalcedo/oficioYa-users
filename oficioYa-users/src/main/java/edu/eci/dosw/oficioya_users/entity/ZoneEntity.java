package edu.eci.dosw.oficioya_users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Barrio del catálogo de zonas. Los ids son fijos (data.sql) porque otros microservicios los guardan*/
@Getter
@Setter
@Entity
@Table(name = "zones")
public class ZoneEntity {

    @Id
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String locality;
}
