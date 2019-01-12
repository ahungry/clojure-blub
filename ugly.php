<?php

/**
;; Business logic
;; Get a user based on matching id.
;; If they're over 18, and favorite language is this one,
;; then send them a job offer invite.
 */
class UserModel {
    private $name;
    private $age;
    private $id;
    private $language;

    public function __construct (string $name, int $age, int $id, string $language) {
        $this->name = $name;
        $this->age = $age;
        $this->id = $id;
        $this->language = $language;
    }

    public function isAdult (): bool {
        return $this->age > 18;
    }

    public function languageFan (): bool {
        return $this->language === 'PHP';
    }

    public function jobOffer (): string {
        return "Job offer for: {$this->name} (imagine an email is sent now).";
    }
}

class UserRepository {
    public function getUserById (int $id) {
        return new UserModel('Matt', 36, 1, 'PHP');
    }
}

class UserService {
    private $repo;

    public function __construct (UserRepository $repo) {
        $this->repo = $repo;
    }

    public function getUserById (int $id) {
        return $this->repo->getUserById($id);
    }

    public function acquireCandidateByIdMaybeHiring (int $id): string {
        $user = $this->getUserById ($id);

        if (empty($user)) {
            return null;
        }

        if (false === $user->isAdult()) {
            return null;
        }

        if (false === $user->languageFan()) {
            return null;
        }

        return $user->jobOffer();
    }
}

$service = new UserService(new UserRepository());
$candidate = $service->acquireCandidateByIdMaybeHiring(2);

if (null !== $candidate) {
    echo "Email is being sent: " . $candidate;
}
