package io.cord3c.ssi.api.did;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authentication {

	private String type;

	private String[] publicKey;
}
