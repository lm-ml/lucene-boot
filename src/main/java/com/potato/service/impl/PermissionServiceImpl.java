package com.potato.service.impl;

import com.potato.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PermissionServiceImpl  implements PermissionService {

    @Override
    public void save() {
        System.out.println("PermissionServiceImpl********************8");
    }
}
