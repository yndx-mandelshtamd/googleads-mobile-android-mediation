// Copyright 2023 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.ads.mediation.pangle;

import android.content.Context;
import com.bytedance.sdk.openadsdk.api.PAGConstant.PAGChildDirectedType;
import com.bytedance.sdk.openadsdk.api.PAGConstant.PAGDoNotSellType;
import com.bytedance.sdk.openadsdk.api.PAGConstant.PAGGDPRConsentType;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAd;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerAdLoadListener;
import com.bytedance.sdk.openadsdk.api.banner.PAGBannerRequest;
import com.bytedance.sdk.openadsdk.api.init.PAGConfig;
import com.bytedance.sdk.openadsdk.api.init.PAGSdk;

/**
 * A wrapper for Pangle SDK's static methods that the adapter calls.
 *
 * <p>This wrapper exists to make it possible to mock Pangle SDK's static methods during unit
 * testing.
 */
public class PangleSdkWrapper {

  public void init(Context context, PAGConfig config, PAGSdk.PAGInitCallback callback) {
    PAGSdk.init(context, config, callback);
  }

  boolean isInitSuccess() {
    return PAGSdk.isInitSuccess();
  }

  void setChildDirected(@PAGChildDirectedType int childDirectedType) {
    PAGConfig.setChildDirected(childDirectedType);
  }

  void setGdprConsent(@PAGGDPRConsentType int gdpr) {
    PAGConfig.setGDPRConsent(gdpr);
  }

  void setDoNotSell(@PAGDoNotSellType int ccpa) {
    PAGConfig.setDoNotSell(ccpa);
  }

  void setUserData(String userData) {
    PAGConfig.setUserData(userData);
  }

  String getBiddingToken() {
    return PAGSdk.getBiddingToken();
  }

  public void loadBannerAd(
      String placementId, PAGBannerRequest request, PAGBannerAdLoadListener listener) {
    PAGBannerAd.loadAd(placementId, request, listener);
  }
}
