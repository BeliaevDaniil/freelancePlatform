package freelanceplatform.dto.entityDTO;

import lombok.Value;

@Value
public class FeedbackDTO {

    Integer id;
    Integer senderId;
    Integer receiverId;
    Integer rating;
    String comment;
}
