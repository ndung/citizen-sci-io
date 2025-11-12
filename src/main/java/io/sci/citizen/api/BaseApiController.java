package io.sci.citizen.api;

import io.sci.citizen.api.component.JwtTokenUtil;
import io.sci.citizen.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class BaseApiController {

	@Autowired
	private JwtTokenUtil tokenUtil;

    protected final static ResponseEntity<Response> FORBIDDEN = new ResponseEntity<Response>(HttpStatus.FORBIDDEN);

    protected String createToken(User user) {
        return tokenUtil.createToken(user);
    }

    protected boolean authorize(String token) {
        String roleTokens = null;
        try {
            roleTokens = tokenUtil.getRoles(token);
            if (roleTokens!=null){
                return true;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    protected boolean authenticate(String token) {
        return tokenUtil.authenticate(token);
    }

    protected String getUserId(String token) {
        return tokenUtil.getUserId(token);
    }


    protected ResponseEntity<Response> getHttpStatus(Response response) {
        HttpStatus hs = response.getData() == null ? HttpStatus.BAD_REQUEST :
                HttpStatus.OK;
        return response(hs, response);
    }

    protected ResponseEntity<Response> response(HttpStatus status, Response response) {
		return new ResponseEntity<>(response, status);
	}

    protected ResponseEntity<Response> getHttpStatus(Response response, HttpHeaders headers) {
        HttpStatus hs = response.getData() == null ? HttpStatus.BAD_REQUEST :
                HttpStatus.OK;
        return new ResponseEntity<>(response, headers, hs);
    }
}
