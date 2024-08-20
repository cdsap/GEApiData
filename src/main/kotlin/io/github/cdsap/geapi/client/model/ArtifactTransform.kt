package io.github.cdsap.geapi.client.model

data class ArtifactTransform(
    val artifactTransformExecutionName: String,
    val transformActionType: String,
    val inputArtifactName: String,
    val outcome: String,
    val avoidanceOutcome: String,
    val duration: String,
    val avoidanceSavings: String? = null,
    val fingerprintingDuration: String,
    val cacheArtifactSize: String,
    val changedAttributes: Array<ChangedAttributes>,
    var buildScanId: String? = null,
)
