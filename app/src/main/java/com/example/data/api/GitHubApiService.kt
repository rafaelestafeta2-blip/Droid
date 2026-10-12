package com.example.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface GitHubApiService {
  @Headers(
    "Accept: application/vnd.github+json",
    "X-GitHub-Api-Version: 2022-11-28"
  )
  @POST("repos/{owner}/{repo}/actions/workflows/{workflow_id}/dispatches")
  suspend fun triggerWorkflowDispatch(
    @Header("Authorization") authHeader: String,
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Path("workflow_id") workflowId: String,
    @Body payload: WorkflowDispatchPayload
  ): Response<ResponseBody>

  @Headers(
    "Accept: application/vnd.github+json",
    "X-GitHub-Api-Version: 2022-11-28"
  )
  @GET("repos/{owner}/{repo}/actions/runs")
  suspend fun getWorkflowRuns(
    @Header("Authorization") authHeader: String,
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Query("per_page") perPage: Int = 5
  ): Response<WorkflowRunsResponse>

  @Headers(
    "Accept: application/vnd.github+json",
    "X-GitHub-Api-Version: 2022-11-28"
  )
  @GET("repos/{owner}/{repo}/actions/runs/{run_id}/jobs")
  suspend fun getWorkflowJobs(
    @Header("Authorization") authHeader: String,
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Path("run_id") runId: Long
  ): Response<WorkflowJobsResponse>

  @Headers(
    "Accept: application/vnd.github+json",
    "X-GitHub-Api-Version: 2022-11-28"
  )
  @GET("repos/{owner}/{repo}/actions/runs/{run_id}/artifacts")
  suspend fun getRunArtifacts(
    @Header("Authorization") authHeader: String,
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Path("run_id") runId: Long
  ): Response<ArtifactsResponse>

  @Headers(
    "Accept: application/vnd.github+json",
    "X-GitHub-Api-Version: 2022-11-28"
  )
  @Streaming
  @GET("repos/{owner}/{repo}/actions/artifacts/{artifact_id}/zip")
  suspend fun downloadArtifactZip(
    @Header("Authorization") authHeader: String,
    @Path("owner") owner: String,
    @Path("repo") repo: String,
    @Path("artifact_id") artifactId: Long
  ): Response<ResponseBody>
}
