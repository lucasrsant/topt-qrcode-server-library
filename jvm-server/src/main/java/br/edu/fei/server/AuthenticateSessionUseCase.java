package br.edu.fei.server;

import br.edu.fei.auth.totp.HashingAlgorithm;
import br.edu.fei.auth.totp.TOTP;
import br.edu.fei.auth.totp.TOTPGenerator;
import br.edu.fei.server.payloads.AuthenticateSessionPayload;

/*package*/ class AuthenticateSessionUseCase {

    private final Repository<String, AuthenticationSession> authenticationSessionRepository;
    private final Repository<String, IdentifiedUserRegistration> userRepository;

    /*package*/ AuthenticateSessionUseCase(Repository<String, AuthenticationSession> authenticationSessionRepository,
                                           Repository<String, IdentifiedUserRegistration> userRepository) {
        this.authenticationSessionRepository = authenticationSessionRepository;
        this.userRepository = userRepository;
    }

    /*package*/ String execute(AuthenticateSessionPayload payload) {
        if(authenticationSessionRepository.contains(payload.sessionId) == false)
            return "not_authenticated";

        if(userRepository.contains(payload.deviceId) == false)
            return "not_authenticated";

        AuthenticationSession session = authenticationSessionRepository.get(payload.sessionId);
        IdentifiedUserRegistration user = userRepository.get(payload.deviceId);

        String oneTimePassword = TOTPGenerator.generate(new TOTP(8, 300, session.getHashKey(), HashingAlgorithm.SHA_256));
        return oneTimePassword.equals(session.getOneTimePassword()) && user.isVerified() ? "authenticated as " + user.email : "not_authenticated";
    }
}
