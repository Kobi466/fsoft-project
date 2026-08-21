package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.entities.UserCardSrs;
import org.springframework.stereotype.Service;

@Service
public interface ReviewAlgorithmService {
     void applyFsrsAlgorithm(UserCardSrs userCardSrs, int grade);
     void applySm2Algorithm(UserCardSrs userCardSrs, int q);
}
