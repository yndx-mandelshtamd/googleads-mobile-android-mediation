package com.google.ads.mediation.adaptertestkit

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.android.gms.ads.AdFormat
import com.google.android.gms.ads.mediation.Adapter
import com.google.android.gms.ads.mediation.InitializationCompleteCallback
import com.google.android.gms.ads.mediation.MediationConfiguration
import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

// region Version Tests
/**
 * Extension function that tests that the return value of [Adapter.getVersionInfo] converted to
 * String is equal to the [expectedValue]. Proper test mock and set up must be done before calling
 * this method.
 */
fun Adapter.assertGetVersionInfo(expectedValue: String) {
  val versionInfo = this.versionInfo

  assertThat(versionInfo.toString()).isEqualTo(expectedValue)
}

/**
 * Extension function that tests that the return value of [Adapter.getSDKVersionInfo] converted to
 * String is equal to the [expectedValue]. Proper test mock and set up must be done before calling
 * this method.
 */
fun Adapter.assertGetSdkVersion(expectedValue: String) {
  val sdkVersionInfo = this.sdkVersionInfo

  assertThat(sdkVersionInfo.toString()).isEqualTo(expectedValue)
}

// endregion

// region Initialize Tests
/**
 * Extension function of [Adapter] that calls its [Adapter.initialize] method using the given
 * parameters and later verifies that [InitializationCompleteCallback.onInitializationFailed] is
 * called once with the [expectedError].
 */
fun Adapter.mediationAdapterInitializeVerifyFailure(
  context: Context,
  initializationCompleteCallback: InitializationCompleteCallback,
  serverParameters: Bundle,
  expectedError: String,
) {
  val configs = listOf(createMediationConfiguration(serverParameters = serverParameters))

  this.initialize(context, initializationCompleteCallback, configs)

  verify(initializationCompleteCallback).onInitializationFailed(expectedError)
}

/**
 * Extension function of [Adapter] that calls its [Adapter.initialize] method using the given
 * parameters and later verifies that no [InitializationCompleteCallback.onInitializationFailed] was
 * triggered.
 */
fun Adapter.mediationAdapterInitializeVerifyNoFailure(
  context: Context,
  initializationCompleteCallback: InitializationCompleteCallback,
  serverParameters: Bundle,
) {
  val configs = listOf(createMediationConfiguration(serverParameters = serverParameters))

  this.initialize(context, initializationCompleteCallback, configs)

  verify(initializationCompleteCallback, never()).onInitializationFailed(any())
}

/**
 * Extension function that calls [Adapter.initialize] method of the given [mediationAdapter] using
 * the parameters and later verifies that [InitializationCompleteCallback.onInitializationSucceeded]
 * was invoked.
 */
fun Adapter.mediationAdapterInitializeVerifySuccess(
  context: Context,
  initializationCompleteCallback: InitializationCompleteCallback,
  serverParameters: Bundle,
) {
  val configs = listOf(createMediationConfiguration(serverParameters = serverParameters))

  this.initialize(context, initializationCompleteCallback, configs)

  verify(initializationCompleteCallback).onInitializationSucceeded()
}

// endregion

// region Helper Functions
/** Returns an instance of [MediationConfiguration] used to initialize the Mediation Adapters. */
fun createMediationConfiguration(
  adFormat: AdFormat = AdFormat.BANNER,
  serverParameters: Bundle = bundleOf(),
): MediationConfiguration {
  return MediationConfiguration(adFormat, serverParameters)
}

// endregion

object AdapterTestKitConstants {
  const val TEST_AD_UNIT = "testAdUnit"
  const val TEST_APP_ID = "testAppId"
}
