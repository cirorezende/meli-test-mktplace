# WireMock Extensions

Custom extension providing random distribution center subsets for the fake external API.

## RandomDistributionCentersTransformer

* Endpoint: `/distribuitioncenters?itemId=...`
* Returns a random subset (size 1..5) of `["SP-001","RJ-001","MG-001","RS-001","PR-001"]`.
* Each call is independent and non-cached at WireMock layer.

## Usage (Docker)

Run WireMock with the extension jar on the classpath:

```bash
wiremock --extensions br.com.ml.mktplace.wiremock.RandomDistributionCentersTransformer
```

The mapping `distribution-centers.json` now references the transformer by name `random-distribution-centers`.
