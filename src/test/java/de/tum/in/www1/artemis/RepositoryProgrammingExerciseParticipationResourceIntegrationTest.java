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
import org.springframework.util.LinkedMultiValueMap;

import de.tum.in.www1.artemis.domain.FileType;
import de.tum.in.www1.artemis.domain.ProgrammingExercise;
import de.tum.in.www1.artemis.domain.participation.ProgrammingExerciseParticipation;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.ProgrammingExerciseRepository;
import de.tum.in.www1.artemis.repository.StudentParticipationRepository;
import de.tum.in.www1.artemis.util.*;
import de.tum.in.www1.artemis.web.rest.dto.FileMove;
import de.tum.in.www1.artemis.web.rest.dto.RepositoryStatusDTO;

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

    private String currentLocalFolderName = "currentFolderName";

    private String newLocalFolderName = "newFolderName";

    private String newLocalFileName = "newFileName";

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

        // add folder to the repository folder
        filePath = Paths.get(localRepository.localRepoFile + "/" + currentLocalFolderName);
        var folder = Files.createDirectory(filePath).toFile();

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

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldGetFile() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("file", currentLocalFileName);
        var file = request.get(repositoryBaseURL + participation.getId() + "/file", HttpStatus.OK, byte[].class, params);
        assertThat(file).isNotEmpty();
        assertThat(new String(file)).isEqualTo(currentLocalFileContent);
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldCreateFile() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("file", "newFile");
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/newFile"))).isFalse();
        request.postWithoutResponseBody(repositoryBaseURL + participation.getId() + "/file", HttpStatus.OK, params);
        assertThat(Files.isRegularFile(Paths.get(localRepository.localRepoFile + "/newFile"))).isTrue();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldCreateFolder() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("folder", "newFolder");
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/newFolder"))).isFalse();
        request.postWithoutResponseBody(repositoryBaseURL + participation.getId() + "/folder", HttpStatus.OK, params);
        assertThat(Files.isDirectory(Paths.get(localRepository.localRepoFile + "/newFolder"))).isTrue();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldRenameFile() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + currentLocalFileName))).isTrue();
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + newLocalFileName))).isFalse();
        FileMove fileMove = new FileMove();
        fileMove.setCurrentFilePath(currentLocalFileName);
        fileMove.setNewFilename(newLocalFileName);
        request.postWithoutLocation(repositoryBaseURL + participation.getId() + "/rename-file", fileMove, HttpStatus.OK, null);
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + currentLocalFileName))).isFalse();
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + newLocalFileName))).isTrue();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldRenameFolder() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + currentLocalFolderName))).isTrue();
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + newLocalFolderName))).isFalse();
        FileMove fileMove = new FileMove();
        fileMove.setCurrentFilePath(currentLocalFolderName);
        fileMove.setNewFilename(newLocalFolderName);
        request.postWithoutLocation(repositoryBaseURL + participation.getId() + "/rename-file", fileMove, HttpStatus.OK, null);
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + currentLocalFolderName))).isFalse();
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + newLocalFolderName))).isTrue();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldDeleteFile() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("file", currentLocalFileName);
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + currentLocalFileName))).isTrue();
        request.delete(repositoryBaseURL + participation.getId() + "/file", HttpStatus.OK, params);
        assertThat(Files.exists(Paths.get(localRepository.localRepoFile + "/" + currentLocalFileName))).isFalse();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldCommitChanges() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        var receivedStatusBeforeCommit = request.get(repositoryBaseURL + participation.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusBeforeCommit.repositoryStatus.toString()).isEqualTo("UNCOMMITTED_CHANGES");
        request.postWithoutLocation(repositoryBaseURL + participation.getId() + "/commit", null, HttpStatus.OK, null);
        var receivedStatusAfterCommit = request.get(repositoryBaseURL + participation.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatusAfterCommit.repositoryStatus.toString()).isEqualTo("CLEAN");
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldPullChanges() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        request.get(repositoryBaseURL + participation.getId() + "/pull", HttpStatus.OK, Void.class);
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void shouldGetStatus() throws Exception {
        StudentParticipation participation = studentParticipationRepository.findAll().get(0);
        var receivedStatus = request.get(repositoryBaseURL + participation.getId(), HttpStatus.OK, RepositoryStatusDTO.class);
        assertThat(receivedStatus).isNotNull();
    }

}
