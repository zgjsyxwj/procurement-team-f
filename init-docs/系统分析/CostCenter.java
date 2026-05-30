package com.cdp.nocebase.domain;

import com.cdp.nocebase.domain.base.SqlBaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * 成本中心实体类
 */
@Getter
@Setter
@Entity
@Table(name = "cost_centers")
public class CostCenter extends SqlBaseEntity {
    
    /**
     * 成本中心编码
     */
    @Column(name = "code")
    private String code;
    
    /**
     * 成本中心名称
     */
    @Column(name = "name")
    private String name;
    
    /**
     * 状态，默认值'1'
     * 0 不启用， 1启用
     */
    @Column(name = "status", columnDefinition = "varchar(255) DEFAULT '1'")
    private String status;
    
    /**
     * 负责人用户ID
     */
    @Column(name = "director_user_id")
    private Long directorUserId;
    
    /**
     * 代理人工号，多个用逗号分隔
     */
    @Column(name = "agent_user_nos")
    private String agentUserNos;
    
}