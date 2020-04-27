package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.in.www1.artemis.domain.FileType;
import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseParticipation;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseRepository;
import de.tum.in.www1.artemis.repository.StudentParticipationRepository;
import de.tum.in.www1.artemis.util.*;

public class RepositoryProgrammingExerciseParticipationResourceIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    private final String repositoryBaseURL = "/api/repository/";

    @Autowired
    private DatabaseUtilService database;

    @Autowired
    private RequestUtilService request;

    @Autowired
    ProgrammingExerciseRepository programmingExerciseRepository;

    @Autowired
    StudentParticipationRepository studentParticipationRepository;

    private String currentLocalFileName = "currentFileName";

    private String currentLocalFileContent = "testContent";

    LocalRepository localRepository = new LocalRepository();

    ProgrammingExercise programmingExercise;

    @BeforeEach
    public void setup() throws Exception {
        database.addUsers(3, 2, 2);
        database.addCourseWithOneProgrammingExerciseAndTestCases();
        programmingExercise = programmingExerciseRepository.findAllWithEagerParticipations().get(0);
        localRepository.configureRepos("localRepo", "originRepo");

        // add file to the repository folder
        Path filePath = Paths.get(localRepository.localRepoFile + "/" + currentLocalFileName);
        var file = Files.createFile(filePath).toFile();
        // write content to the created file
        FileUtils.write(file, currentLocalFileContent);

        var localRepoUrl = new GitUtilService.MockFileRepositoryUrl(localRepository.originRepoFile);
        database.addStudentParticipationForProgrammingExerciseForLocalRepo(programmingExercise, "student1", localRepoUrl.getURL());
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        doReturn(gitService.getRepositoryByLocalPath(localRepository.localRepoFile.toPath())).when(gitService)
                .getOrCheckoutRepository(((ProgrammingExerciseParticipation) participation).getRepositoryUrlAsUrl(), true);
    }

    @AfterEach
    public void tearDown() throws IOException {
        database.resetDatabase();
        LocalRepository.resetLocalRepo(localRepository);
        reset(gitService);
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldGetFiles() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        var files = request.getMap(repositoryBaseURL + participation.getId() + "/files", HttpStatus.OK, String.class, FileType.class);
        assertThat(files).isNotEmpty();
        assertThat(files.containsKey(currentLocalFileName)).isTrue();
    }

}
