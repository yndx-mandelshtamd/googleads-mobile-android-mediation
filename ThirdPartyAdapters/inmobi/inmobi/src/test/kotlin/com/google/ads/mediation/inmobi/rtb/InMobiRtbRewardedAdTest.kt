package com.google.ads.mediation.inmobi.rtb

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.ads.mediation.inmobi.InMobiAdFactory
import com.google.ads.mediation.inmobi.InMobiAdapterUtils
import com.google.ads.mediation.inmobi.InMobiConstants
import com.google.ads.mediation.inmobi.InMobiInitializer
import com.google.ads.mediation.inmobi.InMobiInterstitialWrapper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationRewardedAd
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.common.truth.Truth
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class InMobiRtbRewardedAdTest {
  private val context = ApplicationProvider.getApplicationContext<Context>()
  private val rewardedAdConfiguration = mock<MediationRewardedAdConfiguration>(){
    on { context } doReturn context
  }
  private val mediationAdLoadCallback =
    mock<MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>>()
  private val inMobiInitializer = mock<InMobiInitializer>()
  private val inMobiAdFactory = mock<InMobiAdFactory>()
  private val inMobiRewardedWrapper = mock<InMobiInterstitialWrapper>()
  private val mediationRewardedAdCallback = mock<MediationRewardedAdCallback>()

  lateinit var rtbRewardedAd: InMobiRtbRewardedAd
  lateinit var adMetaInfo: AdMetaInfo

  @Before
  fun setUp() {
    adMetaInfo = AdMetaInfo("fake", null)
    whenever(mediationAdLoadCallback.onSuccess(any())).thenReturn(mediationRewardedAdCallback)

    rtbRewardedAd =
      InMobiRtbRewardedAd(
        rewardedAdConfiguration,
        mediationAdLoadCallback,
        inMobiInitializer,
        inMobiAdFactory
      )
  }

  @Test
  fun onShowAd_ifRewardedAdNotReady_invokesOnAdFailedToShow() {
    whenever(inMobiAdFactory.createInMobiInterstitialWrapper(any(), any(), any()))
      .thenReturn(inMobiRewardedWrapper)
    whenever(inMobiRewardedWrapper.isReady).thenReturn(false)
    whenever(rewardedAdConfiguration.bidResponse).thenReturn("BiddingToken")

    val placementId = 67890L
    // invoke the create rewardedAd method to get an instance of InMobiRewardedWrapper
    rtbRewardedAd.createAndLoadRewardAd(context, placementId, mediationAdLoadCallback)
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.showAd(context)

    val captor = argumentCaptor<AdError>()
    verify(mediationRewardedAdCallback).onAdFailedToShow(captor.capture())
    Truth.assertThat(captor.firstValue.code).isEqualTo(InMobiConstants.ERROR_AD_NOT_READY)
    Truth.assertThat(captor.firstValue.domain).isEqualTo(InMobiConstants.ERROR_DOMAIN)
  }

  @Test
  fun onShowAd_ifRewardedAdIsReady_AdIsShown() {
    whenever(inMobiAdFactory.createInMobiInterstitialWrapper(any(), any(), any()))
      .thenReturn(inMobiRewardedWrapper)
    whenever(inMobiRewardedWrapper.isReady).thenReturn(true)
    whenever(rewardedAdConfiguration.bidResponse).thenReturn("BiddingToken")

    val placementId = 67890L
    // invoke the create rewardedAd method to get an instance of InMobiRewardedWrapper
    rtbRewardedAd.createAndLoadRewardAd(context, placementId, mediationAdLoadCallback)
    rtbRewardedAd.showAd(context)

    verify(inMobiRewardedWrapper).show()
  }

  @Test
  fun onAdDisplayed_invokesOnAdOpenedAndOnVideoStartCallback() {
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onAdDisplayed(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)

    verify(mediationRewardedAdCallback).onAdOpened()
    verify(mediationRewardedAdCallback).onVideoStart()
  }

  @Test
  fun onAdDisplayFailed_invokesOnAdFailedToShowCallback() {
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onAdDisplayFailed(inMobiRewardedWrapper.inMobiInterstitial)

    val captor = argumentCaptor<AdError>()
    verify(mediationRewardedAdCallback).onAdFailedToShow(captor.capture())
    Truth.assertThat(captor.firstValue.code).isEqualTo(InMobiConstants.ERROR_AD_DISPLAY_FAILED)
    Truth.assertThat(captor.firstValue.domain).isEqualTo(InMobiConstants.ERROR_DOMAIN)
  }

  @Test
  fun onAdDismissed_invokesOnAdClosedCallback() {
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onAdDismissed(inMobiRewardedWrapper.inMobiInterstitial)

    verify(mediationRewardedAdCallback).onAdClosed()
  }

  @Test
  fun onRewardsUnlocked_onEmptyRewards_invokesOnUserEarnedRewardWithZeroReward() {
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onRewardsUnlocked(inMobiRewardedWrapper.inMobiInterstitial, null)

    verify(mediationRewardedAdCallback).onVideoComplete()
    val captor = argumentCaptor<RewardItem>()
    verify(mediationRewardedAdCallback).onUserEarnedReward(captor.capture())
    Truth.assertThat(captor.firstValue.type).isEmpty()
    Truth.assertThat(captor.firstValue.amount).isEqualTo(0)
  }

  @Test
  fun onRewardsUnlocked_invokesOnUserEarnedReward() {
    val expectedRewardType = "SecondReward"
    val expectedReward = "2"
    var rewards = mapOf<Any, Any>("firstReward" to "", expectedRewardType to expectedReward)

    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onRewardsUnlocked(inMobiRewardedWrapper.inMobiInterstitial, rewards)

    verify(mediationRewardedAdCallback).onVideoComplete()
    val captor = argumentCaptor<RewardItem>()
    verify(mediationRewardedAdCallback).onUserEarnedReward(captor.capture())
    Truth.assertThat(captor.firstValue.type).isEqualTo(expectedRewardType)
    Truth.assertThat(captor.firstValue.amount).isEqualTo(expectedReward.toInt())
  }

  @Test
  fun onRewardsUnlocked_onInvalidRewardValue_invokesOnUserEarnedRewardWithDefaultReward() {
    val expectedRewardType = "SecondReward"
    val invalidReward = "invalid"
    val defaultReward = 1
    var rewards = mapOf<Any, Any>(expectedRewardType to invalidReward, "firstReward" to "10")

    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onRewardsUnlocked(inMobiRewardedWrapper.inMobiInterstitial, rewards)

    verify(mediationRewardedAdCallback).onVideoComplete()
    val captor = argumentCaptor<RewardItem>()
    verify(mediationRewardedAdCallback).onUserEarnedReward(captor.capture())
    Truth.assertThat(captor.firstValue.type).isEqualTo(expectedRewardType)
    Truth.assertThat(captor.firstValue.amount).isEqualTo(defaultReward)
  }

  @Test
  fun onAdClicked_invokesReportAdClicked() {
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onAdClicked(inMobiRewardedWrapper.inMobiInterstitial, null)

    verify(mediationRewardedAdCallback).reportAdClicked()
  }

  @Test
  fun onAdImpression_invokesReportAdImpression() {
    // mimic an ad load
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)
    rtbRewardedAd.onAdImpression(inMobiRewardedWrapper.inMobiInterstitial)

    verify(mediationRewardedAdCallback).reportAdImpression()
  }

  @Test
  fun onAdLoadFailed_invokesOnFailureCallback() {
    var inMobiAdRequestStatus =
      InMobiAdRequestStatus(InMobiAdRequestStatus.StatusCode.INTERNAL_ERROR)

    rtbRewardedAd.onAdLoadFailed(inMobiRewardedWrapper.inMobiInterstitial, inMobiAdRequestStatus)

    val captor = argumentCaptor<AdError>()
    verify(mediationAdLoadCallback).onFailure(captor.capture())
    Truth.assertThat(captor.firstValue.code)
      .isEqualTo(InMobiAdapterUtils.getMediationErrorCode(inMobiAdRequestStatus))
    Truth.assertThat(captor.firstValue.domain).isEqualTo(InMobiConstants.INMOBI_SDK_ERROR_DOMAIN)
  }

  @Test
  fun onAdLoadSucceeded_invokesOnSuccessCallback() {
    rtbRewardedAd.onAdLoadSucceeded(inMobiRewardedWrapper.inMobiInterstitial, adMetaInfo)

    verify(mediationAdLoadCallback).onSuccess(ArgumentMatchers.any(rtbRewardedAd::class.java))
  }
}
