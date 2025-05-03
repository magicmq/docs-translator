package dev.magicmq.docstranslator;


import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenResolver {

    private static final Logger logger = LoggerFactory.getLogger(MavenResolver.class);

    private final RepositorySystem system;
    private final RepositorySystemSession session;
    private final List<RemoteRepository> remoteRepositories;
    private final String dependencyScope;

    public MavenResolver(File localRepository, boolean useCentral, String dependencyScope) {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(org.eclipse.aether.spi.connector.RepositoryConnectorFactory.class,
                org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory.class);
        locator.addService(org.eclipse.aether.spi.connector.transport.TransporterFactory.class,
                org.eclipse.aether.transport.http.HttpTransporterFactory.class);
        locator.addService(org.eclipse.aether.spi.connector.transport.TransporterFactory.class,
                org.eclipse.aether.transport.file.FileTransporterFactory.class);
        this.system = locator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(localRepository);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        this.session = session;

        this.remoteRepositories = new ArrayList<>();
        if (useCentral)
            this.remoteRepositories.add(new RemoteRepository.Builder(
                    "central",
                    "default",
                    "https://repo.maven.apache.org/maven2/").build());

        this.dependencyScope = dependencyScope;
    }

    public void addRemoteRepository(String id, String url) {
        this.remoteRepositories.add(new RemoteRepository.Builder(id, "default", url).build());
    }

    public List<Artifact> fetch(List<String> artifacts, List<String> exclusions) {
        List<Artifact> toReturn = new ArrayList<>();

        for (String artifact : artifacts) {
            try {
                Artifact rootArtifact = new DefaultArtifact(artifact);
                Dependency dependency = new Dependency(rootArtifact, dependencyScope);

                CollectRequest collectRequest = new CollectRequest(dependency, remoteRepositories);

                DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
                DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

                List<Artifact> allArtifacts = new ArrayList<>(dependencyResult.getArtifactResults().stream().map(ArtifactResult::getArtifact).toList());

                allArtifacts.removeIf(toCheck ->
                        exclusions.contains(toCheck.getGroupId() + ":" + toCheck.getArtifactId()) || exclusions.contains(toCheck.getGroupId()));

                toReturn.addAll(fetchDependencies(allArtifacts));
            } catch (DependencyResolutionException e) {
                logger.error("Error when resolving dependencies for artifact '{}'. Skipping...", artifact, e);
            }
        }

        return toReturn;
    }

    private List<Artifact> fetchDependencies(List<Artifact> artifacts) {
        List<Artifact> sourceResults = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            Artifact sourcesArtifact = new DefaultArtifact(
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    "sources",
                    "jar",
                    artifact.getVersion()
            );
            try {
                ArtifactRequest sourcesRequest = new ArtifactRequest();
                sourcesRequest.setArtifact(sourcesArtifact);
                sourcesRequest.setRepositories(remoteRepositories);
                sourceResults.add(system.resolveArtifact(session, sourcesRequest).getArtifact());
                logger.info("Fetched JAR artifact {}", artifact);
            } catch (ArtifactResolutionException e) {
                logger.error("Error when resolving dependency artifact '{}'. Skipping...", artifact, e);
            }
        }
        return sourceResults;
    }
}
