package freelanceplatform.dto.readUpdate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProposalReadUpdate {

    private Integer id;
    private Integer freelancerId;
    private Integer taskId;
}
