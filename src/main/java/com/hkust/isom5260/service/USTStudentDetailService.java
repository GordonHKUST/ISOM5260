package com.hkust.isom5260.service;

import com.hkust.isom5260.model.CustomUserDetails;
import com.hkust.isom5260.model.USTStudent;
import com.hkust.isom5260.mapper.PSSUSUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class USTStudentDetailService implements UserDetailsService {

    @Autowired
    private PSSUSUserMapper PSSUSUserMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        USTStudent user = PSSUSUserMapper.selectByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new CustomUserDetails(user);
    }

}

