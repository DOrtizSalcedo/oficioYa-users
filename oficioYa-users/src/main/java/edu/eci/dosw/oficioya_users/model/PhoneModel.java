package edu.eci.dosw.oficioya_users.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PhoneModel {
    private UUID id;
    private String number;
    private boolean principal;
}