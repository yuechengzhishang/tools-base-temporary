/*
 * Copyright (C) 2013 The Android Open Source Project
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
package com.android.build.gradle.internal.variant;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.android.annotations.VisibleForTesting;
import com.android.build.FilterData;
import com.android.build.OutputFile;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.api.AndroidSourceSet;
import com.android.build.gradle.internal.StringHelper;
import com.android.build.gradle.internal.TaskManager;
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.coverage.JacocoInstrumentTask;
import com.android.build.gradle.internal.dependency.VariantDependencies;
import com.android.build.gradle.internal.tasks.CheckManifest;
import com.android.build.gradle.internal.tasks.GenerateApkDataTask;
import com.android.build.gradle.internal.tasks.PrepareDependenciesTask;
import com.android.build.gradle.tasks.AidlCompile;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.android.build.gradle.tasks.GenerateResValues;
import com.android.build.gradle.tasks.JackTask;
import com.android.build.gradle.tasks.MergeAssets;
import com.android.build.gradle.tasks.MergeResources;
import com.android.build.gradle.tasks.NdkCompile;
import com.android.build.gradle.tasks.ProcessAndroidResources;
import com.android.build.gradle.tasks.RenderscriptCompile;
import com.android.builder.core.VariantType;
import com.android.builder.model.SourceProvider;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.gradle.api.Task;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base data about a variant.
 */
public abstract class BaseVariantData<T extends BaseVariantOutputData> {

    public enum SplitHandlingPolicy {
        /**
         * Any release before L will create fake splits where each split will be the entire
         * application with the split specific resources.
         */
        PRE_21_POLICY,

        /**
         * Android L and after, the splits are pure splits where splits only contain resources
         * specific to the split characteristics.
         */
        RELEASE_21_AND_AFTER_POLICY
    }


    @NonNull
    protected final BaseExtension baseExtension;
    @NonNull
    protected final TaskManager taskManager;
    @NonNull
    private final GradleVariantConfiguration variantConfiguration;

    private VariantDependencies variantDependency;

    public Task preBuildTask;
    public PrepareDependenciesTask prepareDependenciesTask;
    public ProcessAndroidResources generateRClassTask;

    public Task sourceGenTask;
    public Task resourceGenTask;
    public Task assetGenTask;
    public CheckManifest checkManifestTask;

    public RenderscriptCompile renderscriptCompileTask;
    public AidlCompile aidlCompileTask;
    public MergeResources mergeResourcesTask;
    public MergeAssets mergeAssetsTask;
    public GenerateBuildConfig generateBuildConfigTask;
    public GenerateResValues generateResValuesTask;
    public Copy copyApkTask;
    public GenerateApkDataTask generateApkDataTask;

    public Copy processJavaResourcesTask;
    public NdkCompile ndkCompileTask;

    public JavaCompile javaCompileTask;
    public JackTask jackTask;
    public Task compileTask;
    public JacocoInstrumentTask jacocoInstrumentTask;
    public Task obfuscationTask;
    public File obfuscatedClassesJar;
    public File mappingFile;

    // Task to assemble the variant and all its output.
    public Task assembleVariantTask;

    private Object[] javaSources;

    private List<File> extraGeneratedSourceFolders;
    private List<File> extraGeneratedResFolders;

    private final List<T> outputs = Lists.newArrayListWithExpectedSize(4);

    /**
     * If true, variant outputs will be considered signed. Only set if you manually set the outputs
     * to point to signed files built by other tasks.
     */
    public boolean outputsAreSigned = false;

    private SplitHandlingPolicy mSplitHandlingPolicy;


    public BaseVariantData(
            @NonNull BaseExtension baseExtension,
            @NonNull TaskManager taskManager,
            @NonNull GradleVariantConfiguration variantConfiguration) {
        this.baseExtension = baseExtension;
        this.variantConfiguration = variantConfiguration;
        this.taskManager = taskManager;

        // eventually, this will require a more open ended comparison.
        mSplitHandlingPolicy =
                baseExtension.getGeneratePureSplits()
                    && variantConfiguration.getMinSdkVersion().getApiLevel() >= 21
                    ? SplitHandlingPolicy.RELEASE_21_AND_AFTER_POLICY
                    : SplitHandlingPolicy.PRE_21_POLICY;

        variantConfiguration.checkSourceProviders();
    }


    public SplitHandlingPolicy getSplitHandlingPolicy() {
        return mSplitHandlingPolicy;
    }

    @NonNull
    protected abstract T doCreateOutput(
            OutputFile.OutputType outputType,
            Collection<FilterData> filters);

    @NonNull
    public T createOutput(OutputFile.OutputType outputType,
            Collection<FilterData> filters) {
        T data = doCreateOutput(outputType, filters);

        // if it's the first time we add an output, mark previous output as part of a multi-output
        // setup.
        if (outputs.size() == 1) {
            outputs.get(0).setMultiOutput(true);
            data.setMultiOutput(true);
        } else if (outputs.size() > 1) {
            data.setMultiOutput(true);
        }

        outputs.add(data);
        return data;
    }

    @NonNull
    public List<T> getOutputs() {
        return outputs;
    }

    @NonNull
    public GradleVariantConfiguration getVariantConfiguration() {
        return variantConfiguration;
    }

    public void setVariantDependency(@NonNull VariantDependencies variantDependency) {
        this.variantDependency = variantDependency;
    }

    @NonNull
    public VariantDependencies getVariantDependency() {
        return variantDependency;
    }

    @NonNull
    public abstract String getDescription();

    @NonNull
    public String getApplicationId() {
        return variantConfiguration.getApplicationId();
    }

    @NonNull
    protected String getCapitalizedBuildTypeName() {
        return StringHelper.capitalize(variantConfiguration.getBuildType().getName());
    }

    @NonNull
    protected String getCapitalizedFlavorName() {
        return StringHelper.capitalize(variantConfiguration.getFlavorName());
    }

    public VariantType getType() {
        return variantConfiguration.getType();
    }

    @NonNull
    public String getName() {
        return variantConfiguration.getFullName();
    }

    @Nullable
    public List<File> getExtraGeneratedSourceFolders() {
        return extraGeneratedSourceFolders;
    }

    @Nullable
    public List<File> getExtraGeneratedResFolders() {
        return extraGeneratedResFolders;
    }

    public void addJavaSourceFoldersToModel(@NonNull File... generatedSourceFolders) {
        if (extraGeneratedSourceFolders == null) {
            extraGeneratedSourceFolders = Lists.newArrayList();
        }

        Collections.addAll(extraGeneratedSourceFolders, generatedSourceFolders);
    }

    public void addJavaSourceFoldersToModel(@NonNull Collection<File> generatedSourceFolders) {
        if (extraGeneratedSourceFolders == null) {
            extraGeneratedSourceFolders = Lists.newArrayList();
        }

        extraGeneratedSourceFolders.addAll(generatedSourceFolders);
    }

    public void registerJavaGeneratingTask(@NonNull Task task, @NonNull File... generatedSourceFolders) {
        sourceGenTask.dependsOn(task);

        for (File f : generatedSourceFolders) {
            javaCompileTask.source(f);
        }

        addJavaSourceFoldersToModel(generatedSourceFolders);
    }

    public void registerJavaGeneratingTask(@NonNull Task task, @NonNull Collection<File> generatedSourceFolders) {
        sourceGenTask.dependsOn(task);

        for (File f : generatedSourceFolders) {
            javaCompileTask.source(f);
        }

        addJavaSourceFoldersToModel(generatedSourceFolders);
    }

   public void registerResGeneratingTask(@NonNull Task task, @NonNull File... generatedResFolders) {
        // no need add the folders anywhere, the convention mapping closure for the MergeResources
        // action will pick them up from here
        resourceGenTask.dependsOn(task);

        if (extraGeneratedResFolders == null) {
            extraGeneratedResFolders = Lists.newArrayList();
        }

        Collections.addAll(extraGeneratedResFolders, generatedResFolders);
    }

    public void registerResGeneratingTask(@NonNull Task task, @NonNull Collection<File> generatedResFolders) {
        // no need add the folders anywhere, the convention mapping closure for the MergeResources
        // action will pick them up from here
        resourceGenTask.dependsOn(task);

        if (extraGeneratedResFolders == null) {
            extraGeneratedResFolders = Lists.newArrayList();
        }

        extraGeneratedResFolders.addAll(generatedResFolders);
    }

    /**
     * Computes the Java sources to use for compilation. This Object[] contains
     * {@link org.gradle.api.file.FileCollection} and {@link File} instances
     */
    @NonNull
    public Object[] getJavaSources() {
        if (javaSources == null) {
            // Build the list of source folders.
            List<Object> sourceList = Lists.newArrayList();

            // First the actual source folders.
            List<SourceProvider> providers = variantConfiguration.getSortedSourceProviders();
            for (SourceProvider provider : providers) {
                sourceList.add(((AndroidSourceSet) provider).getJava().getSourceFiles());
            }

            // then all the generated src folders.
            if (generateRClassTask != null) {
                sourceList.add(generateRClassTask.getSourceOutputDir());
            }

            // for the other, there's no duplicate so no issue.
            if (generateBuildConfigTask != null) {
                sourceList.add(generateBuildConfigTask.getSourceOutputDir());
            }

            if (aidlCompileTask != null) {
                sourceList.add(aidlCompileTask.getSourceOutputDir());
            }

            if (!variantConfiguration.getRenderscriptNdkModeEnabled()
                    && renderscriptCompileTask != null) {
                sourceList.add(renderscriptCompileTask.getSourceOutputDir());
            }

            javaSources = sourceList.toArray();
        }

        return javaSources;
    }

    /**
     * Returns the Java folders needed for code coverage report.
     *
     * This includes all the source folders except for the ones containing R and buildConfig.
     */
    @NonNull
    public List<File> getJavaSourceFoldersForCoverage() {
        // Build the list of source folders.
        List<File> sourceFolders = Lists.newArrayList();

        // First the actual source folders.
        List<SourceProvider> providers = variantConfiguration.getSortedSourceProviders();
        for (SourceProvider provider : providers) {
            for (File sourceFolder : provider.getJavaDirectories()) {
                if (sourceFolder.isDirectory()) {
                    sourceFolders.add(sourceFolder);
                }
            }
        }

        File sourceFolder;
        // then all the generated src folders, except the ones for the R/Manifest and
        // BuildConfig classes.
        sourceFolder = aidlCompileTask.getSourceOutputDir();
        if (sourceFolder.isDirectory()) {
            sourceFolders.add(sourceFolder);
        }

        if (!variantConfiguration.getRenderscriptNdkModeEnabled()) {
            sourceFolder = renderscriptCompileTask.getSourceOutputDir();
            if (sourceFolder.isDirectory()) {
                sourceFolders.add(sourceFolder);
            }
        }

        return sourceFolders;
    }

    /**
     * Returns a list of configuration name for wear connection, from highest to lowest priority.
     * @return list of config.
     */
    @NonNull
    public List<String> getWearConfigNames() {
        List<SourceProvider> providers = variantConfiguration.getSortedSourceProviders();

        // this is the wrong order, so let's reverse it as we gather the names.
        final int count = providers.size();
        List<String> names = Lists.newArrayListWithCapacity(count);
        for (int i = count - 1 ; i >= 0; i--) {
            DefaultAndroidSourceSet sourceSet = (DefaultAndroidSourceSet) providers.get(i);

            names.add(sourceSet.getWearAppConfigurationName());
        }

        return names;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .addValue(variantConfiguration.getFullName())
                .toString();
    }
}
