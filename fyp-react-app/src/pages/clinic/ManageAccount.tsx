import { ChangeEvent, useEffect, useState } from "react";
import { CgSearch } from "react-icons/cg";
import { Link } from "react-router-dom";
import useAxiosPrivate from "../../hooks/useAxiosPrivate";

export default function ManageAccount() {
  const axiosPrivate = useAxiosPrivate();
  const [data, setData] = useState([]);
  const [searchInput, setSearchInput] = useState("");

  useEffect(() => {
    let isMounted = true;
    const controller = new AbortController();
    const fetchData = async () => {
      const response = await axiosPrivate.get("/clinicOwner/getAllStaffs", {
        signal: controller.signal,
      });

      console.log(response.data);
      isMounted && setData(response.data);
    };

    fetchData();

    return () => {
      isMounted = false;
      controller.abort();
    };
  }, []);

  return (
    <>
      <h1>Manage Account</h1>
      <div>
        <div className="input-group mb-3">
          <input
            type="text"
            className="form-control"
            placeholder={`Search Clinic`}
            onChange={(e: ChangeEvent<HTMLInputElement>) =>
              setSearchInput(e.target.value)
            }
            aria-label="Search"
            aria-describedby="Search"
          />
          <button
            className="btn btn-outline-secondary"
            type="button"
            id="button-addon2"
          >
            <CgSearch />
            <span className="ms-2">Search</span>
          </button>
        </div>
        <table
          style={{ background: "white", borderRadius: "1rem" }}
          className="mt-2 table table-striped"
        >
          <thead>
            <tr>
              <th scope="col">#</th>
              <th scope="col">Personnel</th>
              <th scope="col">Role</th>
              <th scope="col">Email</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.length < 1 && (
              <tr>
                <td colSpan={5}>No data available for viewing...</td>
              </tr>
            )}
            {data
              .filter(
                (data) =>
                  JSON.stringify(data).toLowerCase().indexOf(searchInput) !== -1
              )
              .map((data: any, idx) => (
                <>
                  <tr key={idx}>
                    <th scope="row">{idx + 1}</th>
                    <td scope="col">{data.name}</td>
                    <td scope="col">{data.role}</td>
                    <td scope="col">{data.email}</td>
                    <td scope="col">Edit</td>
                  </tr>
                </>
              ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
