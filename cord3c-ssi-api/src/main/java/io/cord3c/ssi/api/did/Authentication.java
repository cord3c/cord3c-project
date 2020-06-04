package io.cord3c.ssi.api.did;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Authentication {

	private String type;

	private List<String> publicKey = new ArrayList<>();
}
