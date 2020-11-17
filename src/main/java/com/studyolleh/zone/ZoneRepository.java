package com.studyolleh.zone;

import com.studyolleh.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Optional<Zone> findByCityAndProvince(String city, String province);
}
