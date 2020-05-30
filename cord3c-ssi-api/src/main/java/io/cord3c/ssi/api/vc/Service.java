package io.cord3c.ssi.api.vc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Service {

	private String type;

	private String serviceEndpoint;
}
