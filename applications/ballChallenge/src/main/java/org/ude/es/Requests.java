package org.ude.es;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping({ "" })
public class Requests {

    @GetMapping({ "/", "/index", "/index.html" })
    public String clientLandingPage(
            Model model
    ) {
        model.addAttribute("abc", BallChallenge.ballChallengeEndpoint);
        return "index";
    }

    @PostMapping("/start")
    public ResponseEntity<Object> startMeasurement() {
        BallChallenge.ballChallengeEndpoint.publishStartMeasurement();
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/setID")
    public ResponseEntity<Object> setEnV5ID(String id) {
        BallChallenge.ballChallengeEndpoint.setEnV5ID(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/requestCountDownUpdate")
    @ResponseBody
    public DataValue requestCountDownUpdate() {
        return new DataValue(BallChallenge.ballChallengeEndpoint.getLastTime());
    }

    @GetMapping("/requestGValueUpdate")
    @ResponseBody
    public DataValue requestData() {
        return new DataValue(BallChallenge.ballChallengeEndpoint.getLastGValue());
    }

    public record DataValue(String VALUE) {}
}
