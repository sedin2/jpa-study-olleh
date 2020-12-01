package com.studyolleh.study;

import com.studyolleh.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

    boolean existsByPath(String path);

    @EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
    Study findByPath(String path);

    @EntityGraph(attributePaths = {"tags", "managers"})
    Study findStudyWithTagsByPath(String path);

    @EntityGraph(attributePaths = {"zones", "managers"})
    Study findStudyWithZonesByPath(String path);

    @EntityGraph(attributePaths = "managers")
    Study findStudyWithManagersByPath(String path);

}
