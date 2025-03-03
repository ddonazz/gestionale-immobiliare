package it.andrea.start.controller.utility;

import java.text.ParseException;
import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import it.andrea.start.dto.JobInfoDTO;
import it.andrea.start.service.JobInfoService;

@RestController
@RequestMapping("/api/job")
@PreAuthorize("hasRole('ADMIN')")
public class JobController {

    private JobInfoService jobInfoService;

    public JobController(JobInfoService jobInfoService) {
	super();
	this.jobInfoService = jobInfoService;
    }

    @Operation(method = "GET", description = "List job defined", summary = "List job defined", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @GetMapping("/list")
    public ResponseEntity<Collection<JobInfoDTO>> listJobs(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language) throws Exception {

	Collection<JobInfoDTO> jobs = jobInfoService.listJobs();

	return ResponseEntity.ok(jobs);
    }

    @Operation(method = "PUT", description = "", summary = "Pianifica un job non in esecuzione", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @PutMapping("/scheduleNewJob/{jobName}")
    public ResponseEntity<Void> scheduleNewJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws Exception {

	jobInfoService.scheduleNewJob(jobName);

	return ResponseEntity.ok().build();
    }

    @Operation(method = "PUT", description = "Update un job in esecuzione", summary = "Update un job in esecuzione", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @PutMapping("/updateScheduleJob/{jobName}")
    public ResponseEntity<Void> updateScheduleJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws ParseException  {
	jobInfoService.updateScheduleJob(jobName);

	return ResponseEntity.ok().build();
    }

    @Operation(method = "PUT", description = "Disattiva un job dal programma", summary = "Disattiva un job dal programma", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @PutMapping("/unScheduleJob/{jobName}")
    public ResponseEntity<Void> unScheduleJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws Exception {

	jobInfoService.unScheduleJob(jobName);

	return ResponseEntity.ok().build();
    }

    @Operation(method = "DELETE", description = "Elimina un job dal programma", summary = "Elimina un job dal programma", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @DeleteMapping("/deleteJob/{jobName}")
    public ResponseEntity<Void> deleteJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws Exception {

	jobInfoService.deleteJob(jobName);

	return ResponseEntity.ok().build();
    }

    @Operation(method = "PUT", description = "Metti in pausa un job", summary = "Metti in pausa un job", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @PutMapping("/pauseJob/{jobName}")
    public ResponseEntity<Void> pauseJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws Exception {

	jobInfoService.pauseJob(jobName);

	return ResponseEntity.ok().build();
    }

    @Operation(method = "PUT", description = "Riprendi un job messo in pausa", summary = "Riprendi un job messo in pausa", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @PutMapping("/resumeJob/{jobName}")
    public ResponseEntity<Void> resumeJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws Exception {

	jobInfoService.resumeJob(jobName);

	return ResponseEntity.ok().build();
    }

    @Operation(method = "PUT", description = "Avvia un job", summary = "Avvia un job", responses = { @ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "401", description = "Not authorized", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))), @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(hidden = true))) })
    @PutMapping("/startJob/{jobName}")
    public ResponseEntity<Void> startJob(@RequestHeader(name = "accept-language", defaultValue = "it", required = false) String language, @PathVariable String jobName) throws Exception {

	jobInfoService.startJobNow(jobName);

	return ResponseEntity.ok().build();
    }

}
