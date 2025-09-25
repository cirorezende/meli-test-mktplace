package br.com.ml.mktplace.wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para garantir que o transformer gera subconjuntos aleatórios válidos.
 */
public class RandomDistributionCentersTransformerTest {

    private final RandomDistributionCentersTransformer transformer = new RandomDistributionCentersTransformer();

    @Test
    @DisplayName("Sempre retorna entre 1 e 5 elementos e nunca vazio")
    void sizeAlwaysBetweenOneAndFive() {
        for (int i = 0; i < 100; i++) {
            ResponseDefinition def = invoke();
            String body = def.getBody();
            String[] arr = parse(body);
            assertThat(arr.length).isBetween(1,5);
        }
    }

    @Test
    @DisplayName("Variação estatística: em várias execuções deve haver pelo menos dois tamanhos diferentes")
    void statisticalVariation() {
        Set<Integer> sizes = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            sizes.add(parse(invoke().getBody()).length);
            if (sizes.size() >= 2) break;
        }
        assertThat(sizes.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Elementos não repetidos no mesmo array")
    void elementsAreUnique() {
        for (int i = 0; i < 30; i++) {
            String[] arr = parse(invoke().getBody());
            assertThat(new HashSet<>(java.util.List.of(arr))).hasSize(arr.length);
        }
    }

    private ResponseDefinition invoke() {
        return transformer.transform(new DummyRequest(), new ResponseDefinitionBuilder().build(), new NoopFileSource(), Parameters.empty(), new NoopAdmin());
    }

    private String[] parse(String json) {
        String trimmed = json.trim();
        // remove [ ] and split by comma handling quotes
        if (trimmed.length() <= 2) return new String[0];
        String inner = trimmed.substring(1, trimmed.length()-1).trim();
        if (inner.isEmpty()) return new String[0];
        String[] raw = inner.split(",");
        for (int i = 0; i < raw.length; i++) {
            String s = raw[i].trim();
            if (s.startsWith("\"")) s = s.substring(1);
            if (s.endsWith("\"")) s = s.substring(0, s.length()-1);
            raw[i] = s;
        }
        return raw;
    }

    // --- Dummies ------------------------------------------------------------
    private static class DummyRequest implements Request {
        @Override public String getUrl() { return "/distribuitioncenters?itemId=XYZ"; }
        @Override public String getAbsoluteUrl() { return getUrl(); }
        @Override public String getMethod() { return "GET"; }
        @Override public String getClientIp() { return "127.0.0.1"; }
        @Override public String getHeader(String key) { return null; }
        @Override public java.util.List<String> getHeaders(String key) { return java.util.Collections.emptyList(); }
        @Override public boolean containsHeader(String key) { return false; }
        @Override public java.util.Set<String> getAllHeaderKeys() { return java.util.Collections.emptySet(); }
        @Override public String getBodyAsString() { return ""; }
        @Override public byte[] getBody() { return new byte[0]; }
        @Override public String getScheme() { return "http"; }
        @Override public String getHost() { return "localhost"; }
        @Override public int getPort() { return 8080; }
        @Override public String getUrlMatcher() { return getUrl(); }
        @Override public boolean isBrowserProxyRequest() { return false; }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getLoggedDateString() { return ""; }
        @Override public String getLoggedDate() { return ""; }
        @Override public boolean isMultipart() { return false; }
        @Override public java.util.Map<String, java.util.FormField> getMultipart() { return java.util.Collections.emptyMap(); }
    }

    private static class NoopFileSource implements FileSource {
        @Override public FileSource child(String s) { return this; }
        @Override public java.io.File getPath() { return new java.io.File("."); }
        @Override public com.github.tomakehurst.wiremock.common.BinaryFile getBinaryFileNamed(String s) { return null; }
        @Override public String getTextFileNamed(String s) { return null; }
        @Override public void createIfNecessary() { }
        @Override public void writeTextFile(String s, String s1) { }
        @Override public void writeBinaryFile(String s, byte[] bytes) { }
        @Override public boolean exists() { return true; }
        @Override public void deleteFile(String s) { }
        @Override public java.util.List<com.github.tomakehurst.wiremock.common.TextFile> listFilesRecursively() { return java.util.Collections.emptyList(); }
    }

    private static class NoopAdmin implements Admin {
        @Override public void resetAll() { }
        @Override public void resetRequests() { }
        @Override public void resetToDefaultMappings() { }
        @Override public void resetScenarios() { }
        @Override public void shutdownServer() { }
        @Override public void saveMappings() { }
        @Override public void createStubMapping(com.github.tomakehurst.wiremock.stubbing.StubMapping stubMapping) { }
        @Override public void editStubMapping(com.github.tomakehurst.wiremock.stubbing.StubMapping stubMapping) { }
        @Override public void removeStubMapping(com.github.tomakehurst.wiremock.stubbing.StubMapping stubMapping) { }
        @Override public void removeStubsByMetadata(com.github.tomakehurst.wiremock.matching.Metadata metadata) { }
        @Override public void removeServeEvent(com.github.tomakehurst.wiremock.verification.ServeEvent serveEvent) { }
        @Override public void updateGlobalSettings(com.github.tomakehurst.wiremock.core.Admin.GlobalSettingsSpec globalSettingsSpec) { }
        @Override public void addSocketAcceptDelay(com.github.tomakehurst.wiremock.extension.Parameters parameters) { }
        @Override public java.util.List<com.github.tomakehurst.wiremock.stubbing.StubMapping> listAllStubMappings() { return java.util.Collections.emptyList(); }
        @Override public com.github.tomakehurst.wiremock.stubbing.StubMapping getStubMapping(com.github.tomakehurst.wiremock.common.Json withUuid) { return null; }
        @Override public java.util.List<com.github.tomakehurst.wiremock.verification.ServeEvent> getServeEvents() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<com.github.tomakehurst.wiremock.verification.ServeEvent> getServeEventsDifferingOnMethod() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<com.github.tomakehurst.wiremock.verification.ServeEvent> getUnmatchedServeEvents() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<com.github.tomakehurst.wiremock.verification.NearMiss> findTopNearMissesFor(com.github.tomakehurst.wiremock.http.Request request) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<com.github.tomakehurst.wiremock.verification.NearMiss> findTopNearMissesFor(com.github.tomakehurst.wiremock.matching.RequestPattern requestPattern) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<com.github.tomakehurst.wiremock.verification.NearMiss> findNearMissesForAllUnmatchedRequests() { return java.util.Collections.emptyList(); }
        @Override public com.github.tomakehurst.wiremock.stubbing.ServeEvent getServeEvent(com.github.tomakehurst.wiremock.common.Json withUuid) { return null; }
        @Override public void removeServeEventsForStubsMatchingMetadata(com.github.tomakehurst.wiremock.matching.Metadata metadata) { }
        @Override public void removeServeEventsForStubsMatchingMetadata(com.github.tomakehurst.wiremock.matching.Metadata metadata, boolean b) { }
        @Override public com.github.tomakehurst.wiremock.admin.model.GetGlobalSettingsResult getGlobalSettings() { return null; }
        @Override public void setGlobalFixedDelay(Integer integer) { }
        @Override public void setGlobalRandomDelayDistribution(com.github.tomakehurst.wiremock.extension.Parameters parameters) { }
        @Override public void setGlobalChunkedDribbleDelay(com.github.tomakehurst.wiremock.extension.Parameters parameters) { }
        @Override public void clearGlobalRandomDelayDistribution() { }
        @Override public com.github.tomakehurst.wiremock.verification.FindServeEventsResult findServeEvents(com.github.tomakehurst.wiremock.verification.FindServeEventsResult request) { return null; }
        @Override public void createStubMappingAndServeExistingStubData(com.github.tomakehurst.wiremock.stubbing.StubMapping stubMapping) { }
    }
}
