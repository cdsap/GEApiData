package io.github.cdsap.geapi.client.model

data class ArtifactTransform(
    val artifactTransformExecutionName: String,
    val transformActionType: String,
    val inputArtifactName: String,
    val outcome: String,
    val avoidanceOutcome: String,
    val duration: String,
    val fingerprintingDuration: String,
    val changedAttributes: Array<ChangedAttributes>
)
