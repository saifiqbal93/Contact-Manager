document.addEventListener("DOMContentLoaded", () => {
console.log("updated")
    const urlParams = new URLSearchParams(window.location.search);

    if (window.location.pathname === "/user/profile" && urlParams.get("profileUpdated") === "true") {
      const popupContainer = document.getElementById("popupContainer");
      const popupTextMessage = document.getElementById("popupTextMessage");
       popupTextMessage.innerText = "Profile updated successfully!";
      popupContainer.classList.remove("hidden");

      setTimeout(() => {
        popupContainer.classList.add("hidden");
      }, 5000);
    }

    else if (window.location.pathname === "/user/profile" && urlParams.get("updatePassword") === "true") {
          const popupContainer = document.getElementById("popupContainer");
          const popupTextMessage = document.getElementById("popupTextMessage");
          popupTextMessage.innerText = "Password updated successfully!";
          popupContainer.classList.remove("hidden");

          setTimeout(() => {
            popupContainer.classList.add("hidden");
          }, 5000);
        }
  });

