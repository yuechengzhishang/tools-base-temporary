/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.build.gradle.internal.variant

import com.android.annotations.NonNull
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.BuildTypeData
import com.android.build.gradle.internal.ProductFlavorData
import com.android.build.gradle.internal.TaskManager
import com.android.build.gradle.internal.VariantModel
import com.android.build.gradle.internal.api.LibraryVariantImpl
import com.android.build.gradle.internal.api.LibraryVariantOutputImpl
import com.android.build.gradle.internal.api.ReadOnlyObjectProvider
import com.android.build.gradle.internal.core.GradleVariantConfiguration
import com.android.builder.core.AndroidBuilder
import com.android.builder.core.VariantType
import com.google.common.collect.Lists
import org.gradle.api.GradleException
import org.gradle.internal.reflect.Instantiator

/**
 */
public class LibraryVariantFactory implements VariantFactory<LibraryVariantData> {

    @NonNull
    Instantiator instantiator
    @NonNull
    private final LibraryExtension extension
    @NonNull
    private final AndroidBuilder androidBuilder;

    public LibraryVariantFactory(
            @NonNull Instantiator instantiator,
            @NonNull AndroidBuilder androidBuilder,
            @NonNull LibraryExtension extension) {
        this.instantiator = instantiator;
        this.androidBuilder = androidBuilder;
        this.extension = extension;
    }

    @Override
    @NonNull
    public LibraryVariantData createVariantData(
            @NonNull GradleVariantConfiguration variantConfiguration,
            @NonNull Set<String> densities,
            @NonNull Set<String> abis,
            @NonNull Set<String> compatibleScreens,
            @NonNull TaskManager taskManager) {
        return new LibraryVariantData(extension, taskManager, variantConfiguration)
    }

    @Override
    @NonNull
    public BaseVariant createVariantApi(
            @NonNull BaseVariantData<? extends BaseVariantOutputData> variantData,
            @NonNull ReadOnlyObjectProvider readOnlyObjectProvider) {
        LibraryVariantImpl variant = instantiator.newInstance(
                LibraryVariantImpl.class, variantData, androidBuilder, readOnlyObjectProvider)

        // now create the output objects
        List<? extends BaseVariantOutputData> outputList = variantData.getOutputs();
        List<BaseVariantOutput> apiOutputList = Lists.newArrayListWithCapacity(outputList.size());

        for (BaseVariantOutputData variantOutputData : outputList) {
            LibVariantOutputData libOutput = (LibVariantOutputData) variantOutputData;

            LibraryVariantOutputImpl output = instantiator.newInstance(
                    LibraryVariantOutputImpl.class, libOutput);

            apiOutputList.add(output);
        }

        variant.addOutputs(apiOutputList);

        return variant
    }

    @NonNull
    @Override
    public VariantType getVariantConfigurationType() {
        return VariantType.LIBRARY
    }

    @Override
    boolean isLibrary() {
        return true
    }

    /***
     * Prevent customization of applicationId or applicationIdSuffix.
     */
    @Override
    public void validateModel(@NonNull VariantModel model) {
        if (model.getDefaultConfig().getProductFlavor().getApplicationId() != null) {
            throw new GradleException("Library projects cannot set applicationId. " +
                    "applicationId is set to '" +
                    model.getDefaultConfig().getProductFlavor().getApplicationId() +
                    "' in default config.");
        }

        for (BuildTypeData buildType : model.getBuildTypes().values()) {
            if (buildType.getBuildType().getApplicationIdSuffix() != null) {
                throw new GradleException("Library projects cannot set applicationId. " +
                        "applicationIdSuffix is set to '" +
                        buildType.getBuildType().getApplicationIdSuffix() +
                        "' in build type '" + buildType.getBuildType().getName() + "'.");
            }
        }
        for (ProductFlavorData productFlavor : model.getProductFlavors().values()) {
            if (productFlavor.getProductFlavor().getApplicationId() != null) {
                throw new GradleException("Library projects cannot set applicationId. " +
                        "applicationId is set to '" +
                        productFlavor.getProductFlavor().getApplicationId() + "' in flavor '" +
                        productFlavor.getProductFlavor().getName() + "'.");
            }
        }

    }
}
