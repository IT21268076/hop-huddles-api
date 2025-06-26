import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentQuestion {

    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    private String questionText;
    private List<String> options; // For multiple choice
    private String correctAnswer;
    private String explanation;
    private String difficulty; // EASY, MEDIUM, HARD
    private Float points;
}

