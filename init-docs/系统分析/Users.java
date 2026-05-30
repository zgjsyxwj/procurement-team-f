package com.cdp.nocebase.domain.sys;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Entity;
import org.apache.commons.lang3.StringUtils;
@Setter
@Getter
@Entity
@Table(name = "users")
public class Users {

    @Id
	@Column(name = "id")
    Long id;
    
    @Column(name = "username")
    String username;
    
    @Column(name = "email")
    String email;
    
    @Column(name = "name_en")
    private String nameEn;
    
    @Column(name = "name_zh")
    private String nameZh;
    //工号
    @Column(name = "workid")
    private String workid;
    @Column(name = "work_email")
    private String workEmail;

    @Column(name = "full_display")
    String fullDisplay;
    @Column(name = "cost_center_id")
    private Long costCenterId;
    
    //直线经理id
    @Column(name = "lm_user_id")
    private Long lmUserId;
}
