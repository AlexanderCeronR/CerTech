package com.certech.config;

import org.springframework.test.context.ActiveProfiles;

/** Loads prod properties against the isolated test app, never the production app or DB. */
@ActiveProfiles("prod")
class ProductionProfileHttpLimitsTest extends EmbeddedHttpLimitsTest {}
